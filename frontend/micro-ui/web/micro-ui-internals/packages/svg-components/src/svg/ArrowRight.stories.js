import React from "react";
import { ArrowRight } from "./ArrowRight";

export default {
  tags: ['autodocs'],
  argTypes: {
    className: {
        options: ['custom-class'],
        control: { type: 'check' },
    }
  },
  title: "ArrowRight",
  component: ArrowRight,
};

export const Default = () => <ArrowRight />;
export const Fill = () => <ArrowRight fill="blue" />;
export const Size = () => <ArrowRight height="50" width="50" />;
export const CustomStyle = () => <ArrowRight style={{ border: "1px solid red" }} />;
export const CustomClassName = () => <ArrowRight className="custom-class" />;
export const Clickable = () => <ArrowRight onClick={()=>console.log("clicked")} />;

const Template = (args) => <ArrowRight {...args}/>;

export const Playground = Template.bind({});
Playground.args = {
  className: 'custom-class',
  style:{ border: "3px solid green" }
};
