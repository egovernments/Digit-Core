import React from "react";
import { ArrowBack } from "./ArrowBack";

export default {
  tags: ['autodocs'],
  argTypes: {
    className: {
        options: ['custom-class'],
        control: { type: 'check' },
    }
  },
  title: "ArrowBack",
  component: ArrowBack,
};

export const Default = () => <ArrowBack />;
export const Fill = () => <ArrowBack fill="blue" />;
export const Size = () => <ArrowBack height="50" width="50" />;
export const CustomStyle = () => <ArrowBack style={{ border: "1px solid red" }} />;
export const CustomClassName = () => <ArrowBack className="custom-class" />;
export const Clickable = () => <ArrowBack onClick={()=>console.log("clicked")} />;

const Template = (args) => <ArrowBack {...args}/>;

export const Playground = Template.bind({});
Playground.args = {
  className: 'custom-class',
  style:{ border: "3px solid green" }
};
