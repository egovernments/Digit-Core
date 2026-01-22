import React from "react";
import { Toc } from "./Toc";

export default {
  tags: ['autodocs'],
  argTypes: {
    className: {
        options: ['custom-class'],
        control: { type: 'check' },
    }
  },
  title: "Toc",
  component: Toc,
};

export const Default = () => <Toc />;
export const Fill = () => <Toc fill="blue" />;
export const Size = () => <Toc height="50" width="50" />;
export const CustomStyle = () => <Toc style={{ border: "1px solid red" }} />;
export const CustomClassName = () => <Toc className="custom-class" />;
export const Clickable = () => <Toc onClick={()=>console.log("clicked")} />;

const Template = (args) => <Toc {...args}/>;

export const Playground = Template.bind({});
Playground.args = {
  className: 'custom-class',
  style:{ border: "3px solid green" }
};
