import React from "react";
import { SubdirectoryArrowRight } from "./SubdirectoryArrowRight";

export default {
  tags: ['autodocs'],
  argTypes: {
    className: {
        options: ['custom-class'],
        control: { type: 'check' },
    }
  },
  title: "SubdirectoryArrowRight",
  component: SubdirectoryArrowRight,
};

export const Default = () => <SubdirectoryArrowRight />;
export const Fill = () => <SubdirectoryArrowRight fill="blue" />;
export const Size = () => <SubdirectoryArrowRight height="50" width="50" />;
export const CustomStyle = () => <SubdirectoryArrowRight style={{ border: "1px solid red" }} />;
export const CustomClassName = () => <SubdirectoryArrowRight className="custom-class" />;

export const Clickable = () => <SubdirectoryArrowRight onClick={()=>console.log("clicked")} />;

const Template = (args) => <SubdirectoryArrowRight {...args} />;

export const Playground = Template.bind({});
Playground.args = {
  className: "custom-class",
  style: { border: "3px solid green" }
};
