import React from "react";
import { Verified } from "./Verified";

export default {
  tags: ['autodocs'],
  argTypes: {
    className: {
        options: ['custom-class'],
        control: { type: 'check' },
    }
  },
  title: "Verified",
  component: Verified,
};

export const Default = () => <Verified />;
export const Fill = () => <Verified fill="blue" />;
export const Size = () => <Verified height="50" width="50" />;
export const CustomStyle = () => <Verified style={{ border: "1px solid red" }} />;
export const CustomClassName = () => <Verified className="custom-class" />;
export const Clickable = () => <Verified onClick={()=>console.log("clicked")} />;

const Template = (args) => <Verified {...args}/>;

export const Playground = Template.bind({});
Playground.args = {
  className: 'custom-class',
  style:{ border: "3px solid green" }
};
